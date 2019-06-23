package jpcasillas.gdl.jal.mx.strategosmx.models;

import java.io.Serializable;

public class FotografiasCensoVO  implements Serializable {

    private Long idfotografiacenso;
    private Long idrepcamcenso;
    private String fotogfrafia;

    public Long getIdfotografiacenso() {
        return idfotografiacenso;
    }

    public void setIdfotografiacenso(Long idfotografiacenso) {
        this.idfotografiacenso = idfotografiacenso;
    }

    public Long getIdrepcamcenso() {
        return idrepcamcenso;
    }

    public void setIdrepcamcenso(Long idrepcamcenso) {
        this.idrepcamcenso = idrepcamcenso;
    }

    public String getFotogfrafia() {
        return fotogfrafia;
    }

    public void setFotogfrafia(String fotogfrafia) {
        this.fotogfrafia = fotogfrafia;
    }
}
