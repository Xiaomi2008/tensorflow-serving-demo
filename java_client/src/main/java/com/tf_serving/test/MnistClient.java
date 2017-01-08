package com.tf_serving.test;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.apache.commons.cli.*;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MnistClient {

    private static final Logger logger = Logger.getLogger(MnistClient.class.getName());
    private final ManagedChannel channel;
    private final PredictionServiceGrpc.PredictionServiceBlockingStub blockingStub;

    // Initialize gRPC client
    public MnistClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        blockingStub = PredictionServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        //System.out.println("Start the predict client");

        String host = "localhost";
        int port = 9000;

        if (args.length == 1) {
            String[] server_pair = args[0].split("=");
            if (!server_pair[0].equals("--server")) {
                System.out.println("you can only specify server address, no other args");
                return;
            }
            String[] server = server_pair[1].split(":");
            host = server[0];
            port = Integer.parseInt(server[1]);
        }

        String modelName = "mnist";
        long modelVersion = 1;

        // Run predict client to send request
        MnistClient client = new MnistClient(host, port);

        try {
            client.do_predict(modelName, modelVersion);
        }
        finally {
            client.shutdown();
        }

        //System.out.println("End of predict client");

    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void do_predict(String modelName, long modelVersion) throws IOException {

        //mnist data
        DataSetIterator mnistTest = new MnistDataSetIterator(1, false, (int)System.currentTimeMillis());

        int totalTest = 1000;
        int error = 0;

        for  (int i = 0; mnistTest.hasNext() && i < totalTest; i++) {

            DataSet ds = mnistTest.next();
            INDArray label = ds.getLabels().getRow(0);
            INDArray feature = ds.getFeatures().getRow(0);

            TensorProto.Builder featuresTensorBuilder = TensorProto.newBuilder();

            for (int j = 0; j < feature.columns(); ++j)
                featuresTensorBuilder.addFloatVal(feature.getFloat(j));


            TensorShapeProto.Dim featuresDim1 = TensorShapeProto.Dim.newBuilder().setSize(1).build();
            TensorShapeProto.Dim featuresDim2 = TensorShapeProto.Dim.newBuilder().setSize(feature.columns()).build();
            TensorShapeProto featuresShape = TensorShapeProto.newBuilder().addDim(featuresDim1).addDim(featuresDim2).build();
            featuresTensorBuilder.setDtype(org.tensorflow.framework.DataType.DT_FLOAT).setTensorShape(featuresShape);
            TensorProto featuresTensorProto = featuresTensorBuilder.build();

            // Generate gRPC request
            com.google.protobuf.Int64Value version = com.google.protobuf.Int64Value.newBuilder().setValue(modelVersion).build();
            Model.ModelSpec modelSpec = Model.ModelSpec.newBuilder().setName(modelName).setVersion(version).build();
            Predict.PredictRequest request = Predict.PredictRequest.newBuilder().setModelSpec(modelSpec)
                    .putInputs("images", featuresTensorProto).build();

            // Request gRPC server
            try {
                Predict.PredictResponse response = blockingStub.predict(request);
                java.util.Map<java.lang.String, org.tensorflow.framework.TensorProto> outputs = response.getOutputsMap();
                TensorProto tp = outputs.get("scores");

                // get true label
                int labelVal = 0;
                for (int cc = 0; cc < label.columns(); cc++) {
                    if (label.getFloat(cc) > 0) {
                        labelVal = cc;
                        break;
                    }
                }

                // get predict label
                int predictVal = 0;
                float predictProb = 0.0f;
                List<Float> probs = tp.getFloatValList();
                for (int cc = 0; cc < probs.size(); cc++) {
                    if (probs.get(cc) > predictProb) {
                        predictProb = probs.get(cc);
                        predictVal = cc;
                    }
                }

                if (labelVal != predictVal) error++;
                System.out.print(".");

            } catch (StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
                return;
            }
        }

        System.out.println("\nInference error rate: " +  String.format("%.2f", (float)error / (float)totalTest * 100.0) + "%" );

    }
}
