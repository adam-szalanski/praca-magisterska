package com.example.bigdataloadingexample.processing;

import java.util.stream.Stream;

public interface ProcessingStrategy {

    void process(Stream<String> data);

}
