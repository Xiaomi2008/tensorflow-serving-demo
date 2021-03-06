// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tensorflow/core/protobuf/config.proto

package org.tensorflow.framework;

public interface RunOptionsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tensorflow.RunOptions)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional .tensorflow.RunOptions.TraceLevel trace_level = 1;</code>
   */
  int getTraceLevelValue();
  /**
   * <code>optional .tensorflow.RunOptions.TraceLevel trace_level = 1;</code>
   */
  org.tensorflow.framework.RunOptions.TraceLevel getTraceLevel();

  /**
   * <pre>
   * Time to wait for operation to complete in milliseconds.
   * </pre>
   *
   * <code>optional int64 timeout_in_ms = 2;</code>
   */
  long getTimeoutInMs();

  /**
   * <pre>
   * The thread pool to use, if session_inter_op_thread_pool is configured.
   * </pre>
   *
   * <code>optional int32 inter_op_thread_pool = 3;</code>
   */
  int getInterOpThreadPool();

  /**
   * <pre>
   * Whether the partition graph(s) executed by the executor(s) should be
   * outputted via RunMetadata.
   * </pre>
   *
   * <code>optional bool output_partition_graphs = 5;</code>
   */
  boolean getOutputPartitionGraphs();

  /**
   * <pre>
   * EXPERIMENTAL.  Options used to initialize DebuggerState, if enabled.
   * </pre>
   *
   * <code>optional .tensorflow.DebugOptions debug_options = 6;</code>
   */
  boolean hasDebugOptions();
  /**
   * <pre>
   * EXPERIMENTAL.  Options used to initialize DebuggerState, if enabled.
   * </pre>
   *
   * <code>optional .tensorflow.DebugOptions debug_options = 6;</code>
   */
  org.tensorflow.framework.DebugOptions getDebugOptions();
  /**
   * <pre>
   * EXPERIMENTAL.  Options used to initialize DebuggerState, if enabled.
   * </pre>
   *
   * <code>optional .tensorflow.DebugOptions debug_options = 6;</code>
   */
  org.tensorflow.framework.DebugOptionsOrBuilder getDebugOptionsOrBuilder();
}
