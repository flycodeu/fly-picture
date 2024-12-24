package com.fly.flyPicture.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author flycode
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
