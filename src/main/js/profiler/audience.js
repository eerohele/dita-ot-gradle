import * as _ from '../utils'
import * as profiler from './common'

export var options = {
  id: 'audience-profiler',
  legend: 'Before we begin, pick the role that best describes you.',
  descriptions: {
    'user': "I don't care about the technical details, I just want to publish things with DITA-OT.",
    'developer': "I'd like to know a bit more about how this thing works."
  }
}

// This would be a protocol in Clojure(Script).
function clickHandler(name, event) {
  profiler.profile(name, event.currentTarget.value)
}

function makeProfilerLabel(name, value) {
  let label = document.createElement('label')
  let role = document.createElement('strong')
  let description = document.createElement('span')

  label.setAttribute('for', name)
  role.textContent = _.toTitleCase(value) + '.'
  description.textContent = options.descriptions[value]

  label.appendChild(role)
  label.appendChild(description)

  return label
}

function makeAudienceProfiler(value, checked) {
  checked = checked || false

  let name = 'audience'
  let div = document.createElement('div')
  let input = document.createElement('input')

  input.setAttribute('type', 'radio')
  input.setAttribute('id', name)
  input.setAttribute('name', name)
  input.setAttribute('value', value)

  input.addEventListener('click', clickHandler.bind(this, name))

  if (checked) {
    input.setAttribute('checked', 'checked')
  }

  let label = makeProfilerLabel(name, value)

  div.appendChild(input)
  div.appendChild(label)

  return div
}

export function initialize(selector, userOptions={}) {
  options = Object.assign(options, userOptions)

  let form = document.createElement('form')
  let fieldset = document.createElement('fieldset')
  let legend = document.createElement('legend')

  let user = makeAudienceProfiler('user', true)
  let developer = makeAudienceProfiler('developer')

  legend.textContent = options.legend

  fieldset.appendChild(legend)
  fieldset.appendChild(user)
  fieldset.appendChild(developer)

  form.setAttribute('id', options.id)
  form.appendChild(fieldset)

  document.querySelector(selector).appendChild(form)

  profiler.profile('audience', 'user')
}
