package org.apache.beam.tutorial.analytic;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.cassandra.CassandraIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.tutorial.config.Person;

import java.util.Arrays;

import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class CassandraDeepStorage {

    public static final void main(String args[]) throws Exception {

        PipelineOptions options = PipelineOptionsFactory.create();

        Pipeline pipeline = Pipeline.create(options);

        PCollection<Person> input = pipeline.apply(
                CassandraIO.<Person>read().withHosts(Arrays.asList("192.168.0.77")).withPort(9042).withKeyspace("beam")
                        .withTable("Person").withEntity(Person.class).withCoder(SerializableCoder.of(Person.class)));

        input.apply("ExtractPayload", ParDo.of(new DoFn<Person, Person>() {
            @ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                c.output(c.element());
            }
        }));

        input.apply(CassandraIO.<Person>write().withHosts(Arrays.asList("192.168.0.77")).withPort(9042)
                .withKeyspace("beam").withEntity(Person.class));

        pipeline.run().waitUntilFinish();
    }
}