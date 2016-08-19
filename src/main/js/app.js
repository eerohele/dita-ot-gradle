import * as audienceProfiler from './profiler/audience'
import * as platformProfiler from './profiler/platform'
import * as visualization from './visualization'

// $(document).ready()
function ready(fn) {
  if (document.readyState !== 'loading') {
    fn()
  } else {
    document.addEventListener('DOMContentLoaded', fn)
  }
}

function initialize() {
  audienceProfiler.initialize('#introduction')
  platformProfiler.initialize('.platform-profiler')
  visualization.initialize('.metrics')
}

ready(initialize)
