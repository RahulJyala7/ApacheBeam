package org.apache.beam.tutorial.analytic;

import com.mongodb.util.JSON;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class MongoIO {

    public static final void main(String args[]) throws Exception {

        PipelineOptions options = PipelineOptionsFactory.create();

        Pipeline pipeline = Pipeline.create(options);

        PCollection<Document> input = pipeline.apply(
                MongoDbIO.read().withUri("mongodb://127.0.0.1:27017").withDatabase("mydb").withCollection("people"));

        input.apply("JUST DEBUG", ParDo.of(new DoFn<Document, JSON>() {
            @ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                System.out.println(c.element().toJson());
            }
        }));

        input.apply(MongoDbIO.write().withUri("mongodb://127.0.0.1:27017").withDatabase("mydb")
                .withCollection("peopleoutput"));

        pipeline.run().waitUntilFinish();
    }
}