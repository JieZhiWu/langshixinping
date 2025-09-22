package com.jiewu.driftbottle.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 7749826257893L;

    private long id;
}
