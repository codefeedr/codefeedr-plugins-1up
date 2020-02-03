package org.tudelft.plugins.cargo.util;

import org.apache.flink.streaming.api.scala.DataStream;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.scala.StreamTableEnvironment;
import org.apache.flink.table.expressions.CallExpression;
import org.apache.flink.table.expressions.Expression;
import org.apache.flink.table.expressions.ValueLiteralExpression;
import org.tudelft.plugins.cargo.protocol.Protocol;
import scala.collection.Seq;
import scala.collection.immutable.List;

import java.util.ArrayList;

public class JavaExpression {
//    public static void fromDataStream2(StreamTableEnvironment tEnv, DataStream<Protocol.CratePojo> stream) {
//        List listA = new ArrayList();
//
//        tEnv.registerDataStream("CargoCrate", stream, new List<Expression>(new ValueLiteralExpression("test")) {
//        };
//    }
}
