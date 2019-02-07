package org.apache.beam.tutorial.analytic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mongodb.util.JSON;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.BigEndianIntegerCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.jdbc.JdbcIO.*;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.io.mongodb.MongoDbIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class CassandraJDBC {

    public static final void main(String args[]) throws Exception {

        PipelineOptions options = PipelineOptionsFactory.create();

        Pipeline pipeline = Pipeline.create(options);

        PCollection<KV<Integer, String>> input = pipeline.apply(JdbcIO.<KV<Integer, String>>read()
                .withDataSourceConfiguration(
                        JdbcIO.DataSourceConfiguration.create("com.github.cassandra.jdbc.CassandraDriver",
                                "jdbc:c*:datastax://192.168.0.77:9042/beam?consistencyLevel=ONE"))
                .withQuery("select personid,personname from person;")
                .withCoder(KvCoder.of(BigEndianIntegerCoder.of(), StringUtf8Coder.of()))
                .withRowMapper(new JdbcIO.RowMapper<KV<Integer, String>>() {
                    public KV<Integer, String> mapRow(ResultSet resultSet) throws Exception {
                        System.out.println(resultSet.getInt(1));
                        return KV.of(resultSet.getInt(1), resultSet.getString(2));
                    }
                }));

        input.apply("JUST DEBUG", ParDo.of(new DoFn<KV<Integer, String>, KV<Integer, String>>() {
            @ProcessElement
            public void processElement(ProcessContext c) throws Exception {
                System.out.println(c.element().getValue());
            }
        }));

        input.apply(JdbcIO.<KV<Integer, String>>write()
                .withDataSourceConfiguration(
                        JdbcIO.DataSourceConfiguration.create("com.github.cassandra.jdbc.CassandraDriver",
                                "jdbc:c*:datastax://192.168.0.77:9042/beam?consistencyLevel=ONE"))
                .withStatement("insert into PersonKV(id , data) values(?, ?)")
                .withPreparedStatementSetter(new JdbcIO.PreparedStatementSetter<KV<Integer, String>>() {
                    public void setParameters(KV<Integer, String> element, PreparedStatement query)
                            throws SQLException {
                        query.setInt(1, element.getKey());
                        query.setString(2, element.getValue());
                    }
                }));

        pipeline.run().waitUntilFinish();
    }
}