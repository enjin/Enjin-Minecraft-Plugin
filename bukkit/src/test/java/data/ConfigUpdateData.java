package data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class ConfigUpdateData {
    @Getter
    @SerializedName(value = "use-buy-gui")
    private Boolean useBuyGui;

    public ConfigUpdateData(Boolean useBuyGui) {
        this.useBuyGui = useBuyGui;
    }
}
