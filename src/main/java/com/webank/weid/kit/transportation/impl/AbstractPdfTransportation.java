

package com.webank.weid.kit.transportation.impl;

import java.util.List;

import com.webank.weid.kit.transportation.impl.AbstractTransportation;
import com.webank.weid.kit.transportation.inf.PdfTransportation;


public abstract class AbstractPdfTransportation
    extends AbstractTransportation
    implements PdfTransportation {

    @Override
    public PdfTransportation specify(List<String> verifierWeIdList) {
        this.setVerifier(verifierWeIdList);
        return this;
    }
}
