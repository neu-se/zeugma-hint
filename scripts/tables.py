import pandas as pd

import report_util


def highlight_max(data, props):
    if data.index.nlevels == 2:
        is_max = data.groupby(level=1).transform('max') == data
    else:
        is_max = data == data.max()
    return is_max.replace({True: props, False: ''})


def style_table(table, precision=3, axis=1):
    return table.style.format(precision=precision, na_rep='---') \
        .apply(lambda x: highlight_max(x, 'background-color: yellow;'), axis=axis)


def pivot(data, col, row, value, col2='time'):
    data = data.pivot(index=[row], values=[value], columns=[col, col2]) \
        .reorder_levels(axis=1, order=[col, col2, None]) \
        .sort_index(axis=1) \
        .sort_index(axis=0) \
        .droplevel(2, axis=1)
    data.index.name = data.index.name.title()
    data.columns.names = [None, None]
    if col2 == 'time':
        data.columns = data.columns.map(lambda l: (l[0], report_util.format_time_delta(l[1])))
    return data


def create_stat_table_frame(data, x, baseline_x, y):
    treatments = sorted(data[x].unique())
    rows = []
    baseline_values = data[data[x] == baseline_x][y]
    sig_level = report_util.compute_sig_level(treatments)
    test, _, _, stat = report_util.get_stat_functions(data, y)
    for treatment in treatments:
        values = data[data[x] == treatment][y]
        sig = ''
        if len(baseline_values) > 0 and test(baseline_values, values) < sig_level:
            sig = 'color: red;'
        rows.append([treatment, stat(values), sig])
    return pd.DataFrame(rows, columns=[x, 'stat', 'sig'])


def create_stat_table(data, x, baseline_x, y, columns):
    groups = data[columns].drop_duplicates().to_dict(orient='records')
    frames = []
    for group in groups:
        frame = create_stat_table_frame(report_util.select(data, **group), x=x, baseline_x=baseline_x, y=y)
        for k, v in group.items():
            frame[k] = v
        frames.append(frame)
    return pd.concat(frames)


def create_coverage_table(data, times, baseline_fuzzer):
    table = create_stat_table(data[data['time'].isin(times)],
                              x='fuzzer', baseline_x=baseline_fuzzer, y='covered_branches',
                              columns=['time', 'subject'])
    stats = pivot(table, 'subject', 'fuzzer', 'stat')
    sigs = pivot(table, 'subject', 'fuzzer', 'sig')
    return style_table(stats, precision=1, axis=0) \
        .apply(lambda _: sigs, axis=None) \
        .set_caption('Branch Coverage.')


def remove_never_detected(data):
    # Remove defects that were not detected in any trial
    detected = data.groupby(['defect'])['time'] \
        .apply(lambda x: (~x.isna()).any()) \
        .to_dict()
    return data[data['defect'].apply(lambda x: detected[x])]


def times_to_detected(data, times):
    data = remove_never_detected(data)
    # Convert detection times into boolean detected by time
    elements = []
    for time in times:
        temp = data.copy()
        temp['detected'] = temp['time'] < time
        temp['time'] = time
        elements.append(temp)
    return pd.concat(elements)


def create_defect_table(data, times, baseline_fuzzer):
    data = times_to_detected(data, times)
    table = create_stat_table(data, x='fuzzer', baseline_x=baseline_fuzzer, y='detected', columns=['time', 'defect'])
    stats = pivot(table, 'defect', 'fuzzer', 'stat')
    sigs = pivot(table, 'defect', 'fuzzer', 'sig')
    return style_table(stats, precision=2, axis=0) \
        .apply(lambda _: sigs, axis=None) \
        .set_caption('Defect Detection Rates.')


def create_coverage_pairwise(data, times):
    return create_pairwise(
        data=data[data['time'].isin(times)],
        x='fuzzer',
        y='covered_branches',
        columns=['subject', 'time'],
        caption_f=lambda subject, time: f'{subject} at {report_util.format_time_delta(time)}.'
    )


def create_defects_pairwise(data, times):
    return create_pairwise(
        data=times_to_detected(data, times),
        x='fuzzer',
        y='detected',
        columns=['defect', 'time'],
        caption_f=lambda defect, time: f'{defect} at {report_util.format_time_delta(time)}.'
    )


def create_pairwise(data, x, y, columns, caption_f):
    groups = data[columns] \
        .drop_duplicates() \
        .sort_values(by=columns) \
        .to_dict(orient='records')
    return [report_util.pairwise_heatmap(report_util.select(data, **group), x, y, caption_f(**group)) for group in
            groups]
