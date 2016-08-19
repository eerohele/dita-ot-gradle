var gulp = require('gulp'),
    postcss = require('gulp-postcss'),
    browserify = require('browserify'),
    babel = require('gulp-babel'),
    atImport = require('postcss-import'),
    cssnext = require('cssnext'),
    sourcemaps = require('gulp-sourcemaps'),
    lost = require('lost'),
    responsiveType = require('postcss-responsive-type'),
    verticalRhythm = require('postcss-vertical-rhythm'),
    nano = require('cssnano'),
    through2 = require('through2');

var paths = {
  jsSource: 'src/main/js/',
  jsDestination: 'docs/',
  cssSource: 'src/main/css/',
  cssDestination: 'docs/'
};

gulp.task('styles', function() {
  return gulp.src(paths.cssSource + '**/*.css')
    .pipe(sourcemaps.init())
    .pipe(postcss([
      atImport(), // Must be first.
      cssnext(),
      verticalRhythm(),
      lost(),
      responsiveType(),
      nano()
    ])
    )
    .pipe(sourcemaps.write('./'))
    .pipe(gulp.dest(paths.cssDestination));
});

gulp.task('scripts', function () {
  return gulp.src(paths.jsSource + 'app.js')
      .pipe(through2.obj(function (file, enc, next) {
          browserify([require.resolve("babel-polyfill"), file.path], {
            debug: process.env.NODE_ENV === 'development' 
          }).transform(require('babelify'))
            .bundle(function (err, res) {
                if (err) { return next(err); }

                file.contents = res;
                next(null, file);
            });
      }))
      .on('error', function (error) {
          console.log(error.stack);
          this.emit('end');
      })
      .pipe(require('gulp-rename')('app.js'))
      .pipe(gulp.dest(paths.jsDestination));

});

// gulp.watch(paths.cssSource + '**/*.css', ['styles']);
// gulp.watch(paths.jsSource + '**/*.js', ['scripts']);

gulp.task('default', ['styles', 'scripts']);
