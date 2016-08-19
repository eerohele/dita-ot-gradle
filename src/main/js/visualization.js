export function initialize(selector) {
  d3.json('data.json', function(data) {
    MG.data_graphic({
      title: "With vs. without DITA-OT Gradle Plugin",
      data: data,
      decimals: 1,
      width: 750,
      height: 208,
      right: 50,
      xax_count: 20,
      target: selector,
      area: false,
      x_accessor: "run",
      y_accessor: "duration",
      small_text: true,
      show_tooltip: false,
      legend: ["Without", "With"]
    });
  });
}
