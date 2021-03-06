# Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
# for details. All rights reserved. Use of this source code is governed by a
# BSD-style license that can be found in the LICENSE file.

# We should be able to make this work on Mac. We need:
# -framework OpenGL -framework GLUT -lm -L /usr/X11/lib

{
  'targets': [
    {
      'target_name': 'mobile_emulator_sample',
      'type': 'none',
      'conditions': [
        ['OS=="linux" or OS=="mac"',
          {
            'dependencies': [
              '../../../runtime/dart-runtime.gyp:emulator_embedder',
              'mobile_emulator',
              'copy_dart_files',
              'decode_png'
            ]
          }
        ]
      ]
    },
    {
      'target_name': 'copy_dart_files',
      'type': 'none',
      'copies': [ {
        'destination': '<(PRODUCT_DIR)',
        'files': [
           '../../../runtime/embedders/openglui/common/gl.dart',
           '../src/openglui_raytrace.dart',
           '../src/openglui_canvas_tests.dart'
        ],
      }],
    },
    {
      'target_name': 'decode_png',
      'type': 'none',
      'actions': [
        {
          'action_name': 'decode',
          'inputs': [
            '../src/chrome.hex'
          ],
          'outputs': [
            '<(PRODUCT_DIR)/chrome.png',
          ],
          'action': [ 'xxd', '-r', '../src/chrome.hex',
                      '<(PRODUCT_DIR)/chrome.png' ]
        }
      ]
    },
    {
      'target_name': 'mobile_emulator',
      'type': 'executable',
      'dependencies': [
        '../../../runtime/dart-runtime.gyp:emulator_embedder',
      ],
      'include_dirs': [
         '../../../runtime',
         '/usr/X11/include',
       ],
       'sources': [
          'mobile_emulator_sample.cc',
       ],
       'conditions': [
         ['OS=="linux"',
           {
             'link_settings': {
               'libraries': [
                 '-Wl,--start-group',
                 '-lskia_core',
                 '-lskia_effects',
                 '-lskia_gr',
                 '-lskia_images',
                 '-lskia_opts',
                 '-lskia_opts_ssse3',
                 '-lskia_ports',
                 '-lskia_sfnt',
                 '-lskia_skgr',
                 '-lskia_utils',
                 '-Wl,--end-group',
                 '-lfontconfig',
                 '-lfreetype',
                 '-lGL',
                 '-lglut',
                 '-lGLU',
                 '-lpng',
                 '-lm',
                 '-lc' ],
              'ldflags': [
                '-Wall',
                '-g',
                # TODO(gram): handle release mode.
                '-Lthird_party/skia-desktop/trunk/out/Debug',
              ],
             },
           }
         ],
         ['OS=="mac"',
           {
             'ldflags': [
               '-framework OpenGL',
               '-framework GLUT',
               '-L /usr/X11/lib'
             ],
           }
        ]
      ]
    }
  ]
}

