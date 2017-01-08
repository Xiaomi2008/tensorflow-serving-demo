
from __future__ import print_function

import sys

# This is a placeholder for a Google-internal import.

import tensorflow as tf

from tensorflow.contrib.session_bundle import exporter
from tensorflow.examples.tutorials.mnist import input_data

tf.app.flags.DEFINE_integer('training_iteration', 1000,
                            'number of training iterations.')
tf.app.flags.DEFINE_integer('export_version', 1, 'version number of the model.')
tf.app.flags.DEFINE_string('work_dir', '/tmp/MNIST_data', 'Working directory.')
FLAGS = tf.app.flags.FLAGS


def main(_):
  if len(sys.argv) < 2 or sys.argv[-1].startswith('-'):
    print('Usage: mnist_export.py [--training_iteration=x] '
          '[--export_version=y] export_dir')
    sys.exit(-1)
  if FLAGS.training_iteration <= 0:
    print('Please specify a positive value for training iteration.')
    sys.exit(-1)
  if FLAGS.export_version <= 0:
    print('Please specify a positive value for version number.')
    sys.exit(-1)

  # Train model
  print('Training model...')
  mnist = input_data.read_data_sets(FLAGS.work_dir, one_hot=True)
  sess = tf.InteractiveSession()
  serialized_tf_example = tf.placeholder(tf.string, name='tf_example')

  feature_configs = {
      'x': tf.FixedLenFeature(shape=[784], dtype=tf.float32),
  }
  tf_example = tf.parse_example(serialized_tf_example, feature_configs)
  x = tf.identity(tf_example['x'], name='x')  # use tf.identity() to assign name
  y_ = tf.placeholder('float', shape=[None, 10])
  w = tf.Variable(tf.zeros([784, 10]))
  b = tf.Variable(tf.zeros([10]))
  sess.run(tf.initialize_all_variables())
  y = tf.nn.softmax(tf.matmul(x, w) + b, name='y')
  cross_entropy = -tf.reduce_sum(y_ * tf.log(y))
  train_step = tf.train.GradientDescentOptimizer(0.01).minimize(cross_entropy)
  values, indices = tf.nn.top_k(y, 10)
  prediction_classes = tf.contrib.lookup.index_to_string(
      tf.to_int64(indices), mapping=tf.constant([str(i) for i in range(10)]))
  for _ in range(FLAGS.training_iteration):
    batch = mnist.train.next_batch(50)
    train_step.run(feed_dict={x: batch[0], y_: batch[1]})
  correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(y_, 1))
  accuracy = tf.reduce_mean(tf.cast(correct_prediction, 'float'))
  print('training accuracy %g' %
        sess.run(accuracy,
                 feed_dict={x: mnist.test.images,
                            y_: mnist.test.labels}))
  print('Done training!')

  # Export model
  # WARNING(break-tutorial-inline-code): The following code snippet is
  # in-lined in tutorials, please update tutorial documents accordingly
  # whenever code changes.
  export_path = sys.argv[-1]
  print('Exporting trained model to %s' % export_path)
  init_op = tf.group(tf.initialize_all_tables(), name='init_op')
  saver = tf.train.Saver(sharded=True)
  model_exporter = exporter.Exporter(saver)
  model_exporter.init(
      sess.graph.as_graph_def(),
      init_op=init_op,
      default_graph_signature=exporter.classification_signature(
          input_tensor=serialized_tf_example,
          classes_tensor=prediction_classes,
          scores_tensor=values),
      named_graph_signatures={
          'inputs': exporter.generic_signature({'images': x}),
          'outputs': exporter.generic_signature({'scores': y})})
  model_exporter.export(export_path, tf.constant(FLAGS.export_version), sess)
  print('Done exporting!')


if __name__ == '__main__':
  tf.app.run()
