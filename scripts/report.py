import os
import pathlib
import sys

import matplotlib as mpl
import matplotlib.pyplot as plt
import pandas as pd
from jinja2 import Environment, FileSystemLoader

import extract
import report_util
import tables


def find_dataset(input_dir, name):
    file = os.path.join(input_dir, f'{name}.csv')
    print(f'Checking for {name} data: {file}.')
    if os.path.isfile(file):
        print(f'\t{name.title()} data found.')
        return report_util.read_timedelta_csv(file)
    else:
        print(f'\t{name.title()} data not found.')
        return None


def plot_coverage(data, subject, cmap):
    fuzzers = sorted(data['fuzzer'].unique())
    if cmap is None:
        cmap = report_util.create_cmap(data, 'fuzzer')
    data = report_util.select(data, subject=subject)
    plt.rcParams["font.family"] = 'sans-serif'
    fig, ax = plt.subplots(figsize=(8, 4))
    stats = data.groupby(by=['time', 'fuzzer'])['covered_branches'] \
        .agg([min, max, 'median']) \
        .reset_index() \
        .sort_values('time')
    for fuzzer in fuzzers:
        color = cmap[fuzzer]
        selected = stats[stats['fuzzer'] == fuzzer]
        times = (selected['time'] / pd.to_timedelta(1, 'm')).tolist()
        ax.plot(times, selected['median'], color=color, label=fuzzer)
        ax.fill_between(times, selected['min'], selected['max'], color=color, alpha=0.2)
    ax.set_xlabel('Time (Minutes)')
    ax.set_ylabel('Covered Branches')
    ax.xaxis.get_major_locator().set_params(integer=True)
    ax.yaxis.get_major_locator().set_params(integer=True)
    ax.set_ylim(bottom=0)
    ax.set_xlim(left=0)
    ax.set_title(subject.title())
    return fig


def create_coverage_plots(data, cmap):
    plots = {}
    for subject in sorted(data['subject'].unique()):
        plot_coverage(data, subject, cmap)
        plots[subject] = report_util.fig_to_html()
    return plots


def get_project_root():
    return os.path.join(pathlib.Path(__file__).resolve().parent.parent)


def write_report(report_file, template_variables):
    print(f'Writing report to {report_file}')
    os.makedirs(pathlib.Path(report_file).parent, exist_ok=True)
    resources_dir = os.path.join(get_project_root(), 'resources')
    report = Environment(loader=FileSystemLoader(resources_dir)) \
        .get_template("report.html") \
        .render(template_variables)
    with open(report_file, 'w') as f:
        f.write(report)


def compute_template_variables(coverage, detections, times, baseline_fuzzer):
    template_variables = {}
    print(f'Creating report')
    template_variables['coverage_table'] = tables.create_coverage_table(coverage, times,
                                                                        baseline_fuzzer).to_html()
    template_variables['coverage_pairwise'] = [t.to_html() for t in tables.create_coverage_pairwise(coverage,
                                                                                                    times)]
    cmap = report_util.create_cmap(coverage, 'fuzzer')
    template_variables['coverage_legend'] = {k: mpl.colors.to_hex(v) for k, v in cmap.items()}
    template_variables['coverage_plots'] = create_coverage_plots(coverage, cmap)
    if detections.empty:
        template_variables['defect_table'] = "<p>No matched defects detected.</p>"
        template_variables['defect_pairwise'] = []
    else:
        template_variables['defect_table'] = tables.create_defect_table(detections, times, baseline_fuzzer).to_html()
        template_variables['defect_pairwise'] = [t.to_html() for t in
                                                 tables.create_defects_pairwise(detections, times)]
    return template_variables


def append_base_data(coverage, detections, included_fuzzers):
    base_coverage = report_util.read_timedelta_csv(os.path.join(get_project_root(), 'data', 'coverage.csv'))
    base_detections = report_util.read_timedelta_csv(os.path.join(get_project_root(), 'data', 'detections.csv'))
    base_coverage = base_coverage[base_coverage['fuzzer'].isin(included_fuzzers)]
    base_detections = base_detections[base_detections['fuzzer'].isin(included_fuzzers)]
    return pd.concat([coverage, base_coverage]), pd.concat([detections, base_detections])


def create_report(input_dir, report_file):
    included_fuzzers = ['Zeugma-X', 'Zeugma-Link']
    times = [pd.to_timedelta(5, 'm'), pd.to_timedelta(3, 'h')]
    coverage = find_dataset(input_dir, 'coverage')
    detections = find_dataset(input_dir, 'detections')
    if coverage is None or detections is None:
        coverage, detections = extract.extract_data(input_dir, input_dir, times)
    coverage, detections = append_base_data(coverage, detections, included_fuzzers)
    write_report(report_file, compute_template_variables(coverage, detections, times, 'Zeugma-X'))


def main():
    create_report(sys.argv[1], sys.argv[2])


if __name__ == "__main__":
    main()
