# tf_serving_demo

mnist client的python和java版本。

TensorFlow Serving官方教程中只提供了python版本，并且是使用bazel直接编译成可执行文件，不利于开发调试。而这里的版本是可以直接运行python文件或是jar文件的。

运行方法如下：

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