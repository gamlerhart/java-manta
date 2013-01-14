# Java Manta Client SDK
[java-manta](http://joyent.github.com/java-manta) is the Java SDK for interacting
with Joyent's Manta system.

At present, this SDK only supports the Manta data plane, and not the Marlin
compute component.

# Installation
You'll need [ Java2
1.7.0.11](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
or higher. Download the [java-manta
jar](https://github.com/joyent/java-manta/blob/master/target/java-manta-1.0.jar)
and add it as a dependency to your Java project.

If you prefer to build from source, you'll also need
[maven](https://maven.apache.org/), and then invoke:

        $mvn package

Which will compile the jar to ./targets/java-manta-1.0.jar

# Usage

You'll need a manta login, an associated rsa key, and its corresponding key
fingerprint.

For detailed usage instructions, consult the provided javadoc.

## Example Get Request

        private static final String URL = "https://manta-beta.joyentcloud.com";
        private static final String LOGIN = "yunong";
        private static final String KEY_PATH = "src/test/java/data/id_rsa";
        private static final String KEY_FINGERPRINT = "04:92:7b:23:bc:08:4f:d7:3b:5a:38:9e:4a:17:2e:df";

        public static void main(String... args) {
                CLIENT = MantaClient.newInstance(URL, LOGIN, KEY_PATH, KEY_FINGERPRINT);
                MantaObject gotObject = CLIENT.get("/yunong/stor/foo");
                String data = MantaUtils.inputStreamToString(gotObject.getDataInputStream());
                System.out.println(data);
        }

For more examples, check the included unit tests.

## Logging

The SDK utilizes [log4j](https://logging.apache.org/log4j/1.2/), and logging
can be configured via the usual methods. The underlying
[google-http-java-client](https://code.google.com/p/google-http-java-client/)
utilizes
[java.util.logging.Logger](http://docs.oracle.com/javase/7/docs/api/java/util/logging/Logger.html),
which can be configured
[accordingly](https://code.google.com/p/google-http-java-client/wiki/HTTP).


## License

The MIT License (MIT)
Copyright (c) 2012 Joyent

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Bugs

See <https://github.com/joyent/java-manta/issues>.

