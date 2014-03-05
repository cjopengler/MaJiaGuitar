/*
 * Copyright (C) 2013 Baidu Inc. All rights reserved.
 */
package com.majia.guitar.data;

import java.util.List;

/**
 * 
 * @author panxu
 * @since 2013-12-17
 */
public interface IGuitarData {
    void updateMusics();
    List<MusicEntity> query();
    void updateVersion(Versions versions);
    Versions getVersions();

}
