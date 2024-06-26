package io.kestra.plugin.minio;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

public class DownloadsTest extends AbstractMinIoTest {

    @Test
    void delete() throws Exception {
        this.createBucket();

        upload("/tasks/");
        upload("/tasks/");

        Downloads task = Downloads.builder()
            .id(DownloadsTest.class.getSimpleName())
            .type(Downloads.class.getName())
            .bucket(this.BUCKET)
            .endpoint(minIOContainer.getS3URL())
            .accessKeyId(minIOContainer.getUserName())
            .secretKeyId(minIOContainer.getPassword())
            .action(Downloads.Action.DELETE)
            .build();

        Downloads.Output run = task.run(runContext(task));

        assertThat(run.getObjects().size(), is(2));
        assertThat(run.getObjects().get(0).getUri().toString(), endsWith(".yml"));
        assertThat(run.getOutputFiles().size(), is(2));

        List list = list().build();
        List.Output listOutput = list.run(runContext(list));
        assertThat(listOutput.getObjects().size(), is(0));
    }

    @Test
    void move() throws Exception {
        this.createBucket();

        upload("/tasks/from");
        upload("/tasks/from");

        Downloads task = Downloads.builder()
            .id(DownloadsTest.class.getSimpleName())
            .type(Downloads.class.getName())
            .bucket("{{bucket}}")
            .endpoint(minIOContainer.getS3URL())
            .accessKeyId(minIOContainer.getUserName())
            .secretKeyId(minIOContainer.getPassword())
            .action(Downloads.Action.MOVE)
            .moveTo(Copy.CopyObject.builder()
                .key("/tasks/move")
                .build()
            )
            .build();

        Downloads.Output run = task.run(runContextFactory.of(Map.of("bucket", this.BUCKET)));

        assertThat(run.getObjects().size(), is(2));
        assertThat(run.getOutputFiles().size(), is(2));

        List list = list().prefix("tasks/from").build();
        List.Output listOutput = list.run(runContext(list));
        assertThat(listOutput.getObjects().size(), is(0));

        list = list().prefix("tasks/move").build();
        listOutput = list.run(runContext(list));
        assertThat(listOutput.getObjects().size(), is(2));
    }

}
