# FontEmbedTest

A small project testing the embedding or bundling of a family of fonts, using as example
the OFL licensed 'Inconsolata' (regular and bold-face). This is to investigate a rendering
problem on macOS Retina with default monospaced font Menlo, where under certain JDK
(OpenJDK 11), turning off fractional metrics has no effect when using `AttributedString`
rendering.

__INCOMPLETE__

See 

- https://stackoverflow.com/questions/59428963/openjdk-11-swing-monospaced-font-rendering-problem-on-macos-retina-despite-turne
- https://stackoverflow.com/questions/24800886/how-to-import-a-custom-java-awt-font-from-a-font-family-with-multiple-ttf-files
