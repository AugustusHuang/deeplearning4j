package org.deeplearning4j.nn.graph.graphnodes;

import org.deeplearning4j.nn.graph.nodes.GraphNode;
import org.deeplearning4j.nn.graph.nodes.MergeNode;
import org.deeplearning4j.nn.graph.nodes.SubsetNode;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestGraphNodes {

    @Test
    public void testMergeNode() {
        Nd4j.getRandom().setSeed(12345);
        GraphNode mergeNode = new MergeNode();

        INDArray first = Nd4j.linspace(0, 11, 12).reshape(3, 4);
        INDArray second = Nd4j.linspace(0, 17, 18).reshape(3, 6).addi(100);

        INDArray out = mergeNode.forward(first, second);
        assertArrayEquals(new int[]{3, 10}, out.shape());

        assertEquals(first, out.get(NDArrayIndex.all(), NDArrayIndex.interval(0, 4)));
        assertEquals(second, out.get(NDArrayIndex.all(), NDArrayIndex.interval(4, 10)));

        INDArray[] backward = mergeNode.backward(out);
        assertEquals(first,backward[0]);
        assertEquals(second, backward[1]);
    }

    @Test
    public void testMergeNodeRNN() {

        Nd4j.getRandom().setSeed(12345);
        GraphNode mergeNode = new MergeNode();

        INDArray first = Nd4j.linspace(0, 59, 60).reshape(3, 4, 5);
        INDArray second = Nd4j.linspace(0, 89, 90).reshape(3, 6, 5).addi(100);

        INDArray out = mergeNode.forward(first, second);
        assertArrayEquals(new int[]{3, 10, 5}, out.shape());

        assertEquals(first, out.get(NDArrayIndex.all(), NDArrayIndex.interval(0, 4), NDArrayIndex.all()));
        assertEquals(second, out.get(NDArrayIndex.all(), NDArrayIndex.interval(4, 10), NDArrayIndex.all()));

        INDArray[] backward = mergeNode.backward(out);
        assertEquals(first,backward[0]);
        assertEquals(second, backward[1]);
    }

    @Test
    public void testCnnDepthMerge() {
        Nd4j.getRandom().setSeed(12345);
        GraphNode mergeNode = new MergeNode();

        INDArray first = Nd4j.linspace(0, 3, 4).reshape(1, 1, 2, 2);
        INDArray second = Nd4j.linspace(0, 3, 4).reshape(1, 1, 2, 2).addi(10);

        INDArray out = mergeNode.forward(first, second);
        assertArrayEquals(new int[]{1, 2, 2, 2}, out.shape());

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(first.getDouble(0, 0, i, j), out.getDouble(0, 0, i, j), 1e-6);
                assertEquals(second.getDouble(0, 0, i, j), out.getDouble(0, 1, i, j), 1e-6);
            }
        }

        INDArray[] backward = mergeNode.backward(out);
        assertEquals(first, backward[0]);
        assertEquals(second, backward[1]);


        //Slightly more complicated test:
        first = Nd4j.linspace(0, 17, 18).reshape(1, 2, 3, 3);
        second = Nd4j.linspace(0, 17, 18).reshape(1, 2, 3, 3).addi(100);

        out = mergeNode.forward(first, second);
        assertArrayEquals(new int[]{1, 4, 3, 3}, out.shape());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(first.getDouble(0, 0, i, j), out.getDouble(0, 0, i, j), 1e-6);
                assertEquals(first.getDouble(0, 1, i, j), out.getDouble(0, 1, i, j), 1e-6);

                assertEquals(second.getDouble(0, 0, i, j), out.getDouble(0, 2, i, j), 1e-6);
                assertEquals(second.getDouble(0, 1, i, j), out.getDouble(0, 3, i, j), 1e-6);
            }
        }

        backward = mergeNode.backward(out);
        assertEquals(first, backward[0]);
        assertEquals(second, backward[1]);
    }

    @Test
    public void testSubsetNode(){
        Nd4j.getRandom().setSeed(12345);
        GraphNode subset = new SubsetNode(4,7);

        INDArray in = Nd4j.rand(5, 10);
        INDArray out = subset.forward(in);
        assertEquals(in.get(NDArrayIndex.all(),NDArrayIndex.interval(4,7,true)),out);

        INDArray backward = subset.backward(out)[0];
        assertEquals(Nd4j.zeros(5,4),backward.get(NDArrayIndex.all(),NDArrayIndex.interval(0,3,true)));
        assertEquals(out, backward.get(NDArrayIndex.all(), NDArrayIndex.interval(4,7,true)));
        assertEquals(Nd4j.zeros(5,2), backward.get(NDArrayIndex.all(), NDArrayIndex.interval(8,9,true)));

        //Test same for CNNs:
        in = Nd4j.rand(new int[]{5, 10, 3, 3});
        out = subset.forward(in);
        assertEquals(in.get(NDArrayIndex.all(),NDArrayIndex.interval(4,7,true), NDArrayIndex.all(), NDArrayIndex.all()),out);

        backward = subset.backward(out)[0];
        assertEquals(Nd4j.zeros(5,4,3,3),backward.get(NDArrayIndex.all(),NDArrayIndex.interval(0,3,true), NDArrayIndex.all(), NDArrayIndex.all()));
        assertEquals(out, backward.get(NDArrayIndex.all(), NDArrayIndex.interval(4,7,true), NDArrayIndex.all(), NDArrayIndex.all()));
        assertEquals(Nd4j.zeros(5,2,3,3), backward.get(NDArrayIndex.all(), NDArrayIndex.interval(8,9,true), NDArrayIndex.all(), NDArrayIndex.all()));
    }
}