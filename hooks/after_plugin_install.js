#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

module.exports = function(context) {
    const gradleFile = path.join(context.opts.projectRoot, 'platforms/android/build.gradle');

    if (fs.existsSync(gradleFile)) {
        fs.readFile(gradleFile, 'utf8', function(err, data) {
            if (err) {
                return console.log(err);
            }

            if (data.indexOf('https://jitpack.io') === -1) {
                const result = data.replace(/allprojects {/, 'allprojects {\n    repositories {\n        maven { url "https://jitpack.io" }\n    }\n');

                fs.writeFile(gradleFile, result, 'utf8', function(err) {
                    if (err) return console.log(err);
                });
            }
        });
    }
};
