package org.apache.pig.backend.hadoop.executionengine.spark;

import org.apache.hadoop.mapred.JobConf;
import org.apache.pig.backend.hadoop.datastorage.ConfigurationUtil;
import org.apache.pig.backend.hadoop.executionengine.physicalLayer.PhysicalOperator;
import org.apache.pig.data.Tuple;
import org.apache.pig.impl.PigContext;
import org.apache.pig.impl.util.ObjectSerializer;
import org.apache.pig.impl.util.UDFContext;
import org.apache.spark.rdd.RDD;

import java.io.IOException;
import java.util.List;

/**
 * 
 * @author zhangbaofeng
 *
 */
public class SparkUtil {
	
	/**
	 * 提取出PigContext里的KV配置和udf包
	 * @param pigContext
	 * @return
	 * @throws IOException
	 */
    public static JobConf newJobConf(PigContext pigContext) throws IOException {
        JobConf jobConf = new JobConf(ConfigurationUtil.toConfiguration(pigContext.getProperties()));
        jobConf.set("pig.pigContext", ObjectSerializer.serialize(pigContext));
        UDFContext.getUDFContext().serialize(jobConf);
        jobConf.set("udf.import.list", ObjectSerializer.serialize(PigContext.getPackageImportList()));
        return jobConf;
    }

    public static void assertPredecessorSize(List<RDD<Tuple>> predecessors, PhysicalOperator physicalOperator, int size) {
        if (predecessors.size() != size) {
            throw new RuntimeException("Should have " + size + " predecessors for " +
                    physicalOperator.getClass() + ". Got : " + predecessors.size());
        }
    }

    public static void assertPredecessorSizeGreaterThan(List<RDD<Tuple>> predecessors, PhysicalOperator physicalOperator, int size) {
        if (predecessors.size() <= size) {
            throw new RuntimeException("Should have greater than" + size + " predecessors for " +
                    physicalOperator.getClass() + ". Got : " + predecessors.size());
        }
    }

    public static  int getParallelism(List<RDD<Tuple>> predecessors, PhysicalOperator physicalOperator) {
        int parallelism = physicalOperator.getRequestedParallelism();
        if (parallelism <= 0) {
            // Parallelism wasn't set in Pig, so set it to whatever Spark thinks is reasonable.
            parallelism = predecessors.get(0).context().defaultParallelism();
        }
        return parallelism;
    }

}
