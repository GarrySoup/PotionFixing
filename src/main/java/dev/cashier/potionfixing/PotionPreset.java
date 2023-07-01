package dev.cashier.potionfixing;

import lombok.Getter;

@Getter
public class PotionPreset {

    double throwMultiplier;
    double fallMultiplier;
    double offset;

    PotionPreset(double throwMultiplier,double fallMultiplier,double offset){
        this.throwMultiplier = throwMultiplier;
        this.fallMultiplier = fallMultiplier;
        this.offset = offset;
    }


}
