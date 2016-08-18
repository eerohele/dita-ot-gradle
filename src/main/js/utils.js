export function forEach(a, fn) {
  return [].forEach.call(a, fn)
}

export function filter(a, fn) {
  return [].filter.call(a, fn)
}

export function toTitleCase(value) {
  return value.substring(0, 1).toUpperCase() + value.substring(1)
}

export function contains(a, b) {
  return a.indexOf(b) > -1
}
