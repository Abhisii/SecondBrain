package com.Backend.SecondBrainProject.api.dto;

import java.io.Serializable;
import java.util.List;

public record AskQueryResponse(String answer, List<String> sources) implements Serializable {}