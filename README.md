
由于TensorFlow Serving官方教程中只提供了python版本，并且是使用bazel直接编译成可执行文件，不利于开发调试。所以开发了这个包含python和java版本的mnist serving client，且包含了protobuf生成文件。

## 安装Tensorflow Serving
早期的Tensorflow Serving是需要源码编译的，如果需要源码编译，参考下列步骤：
* 安装jdk1.8，然后用jdk1.8编译bazel
* 装grpc：pip install grpcio
* 如果系统没有curl-devel，需要[自行下载](http://cygwin.mirror.constant.com/x86_64/release/curl/libcurl-devel/)，然后设置`C_INCLUDE_PATH`和`CPLUS_INCLUDE_PATH`，把include路径加进去。
* 下载Tensorflow，到tensorflow目录下运行configure，然后编译`bazel build -c opt  tensorflow_serving/... |  bazel test-c opt  tensorflow_serving/...`

## 运行方法

首先参考TensorFlow Serving官方教程可以用mnist_export.py导出模型，并且启动model server。默认9000端口。

然后需要把protobuf编译为python和java文件。本项目中已经编译好，可以直接使用，省去了这一步骤。

protobuf编译为python方法：

* `pip install grpcio-tools`
* 把tensorflow serving代码做一些微调，让目录结构可以通过编译。tensorflow_serving/apis里面用到的3个proto放到外层tensorflow目录下（里面还有一个tensorflow目录，也就是proto跟这个内层tensorflow平行），并且把代码里面import tensorflow_serving/apis 这个前缀去掉。protoc编译时建议指定绝对路径。
* `python -m grpc.tools.protoc -I./ --python_out=/mypath --grpc_python_out=/mypath *.proto`

protobuf编译为java方法：

* 可以使用protoc安装grpc-java插件的方法，也可以使用maven插件生成，这里使用后者。
* 项目的目录结构可以参考插件的[官方说明][1]，直接把tensorflow serving代码中proto文件复制到项目中的相应目录里。pom的模板（包括java client的代码）都参考了[deep_recommend_system][2]项目。如果需要自己编译生成，可以直接用本项目中的pom，已经包含相应插件。
* 编译完成之后的文件（target/generated-source）加入到client项目中使用即可。

最后，运行客户端：

* python客户端运行参考官方教程即可：`python mnist_client.py --server=localhost:9000`
* java客户端打包之后运行类似命令：`java -jar target/tf_java_predict-1.0-SNAPSHOT.jar --server=localhost:9000` 可以得到跟python客户端类似的输出效果。

[1]:https://www.xolstice.org/protobuf-maven-plugin/usage.html
[2]:https://github.com/tobegit3hub/deep_recommend_system/tree/master/java_predict_client
