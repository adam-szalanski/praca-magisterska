package com.example.bigdataloadingexample.config;


import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;

@MapperConfig(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface DefaultMapstructConfig {

}
