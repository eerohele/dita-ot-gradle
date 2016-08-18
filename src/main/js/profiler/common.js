import * as _ from '../utils'

function dataAttributeContainsValue(el, name, value) {
  return el.getAttribute('data-' + name).split(' ').indexOf(value) === -1
}

export function profile(name, value) {
  var elements = document.querySelectorAll('[data-' + name + ']')

  _.forEach(elements, function(el, i) {
    el.classList.toggle('hidden', dataAttributeContainsValue(el, name, value))
  })
}
