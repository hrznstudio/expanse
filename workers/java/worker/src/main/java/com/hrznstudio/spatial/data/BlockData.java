package com.hrznstudio.spatial.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BlockData {

    @SerializedName("type")
    @Expose
    public int type;
    @SerializedName("meta")
    @Expose
    public int meta;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("text_type")
    @Expose
    public String textType;

}
