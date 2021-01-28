package org.github.iamxwaa.metric;

import java.rmi.RemoteException;

import x.rmi.Rmi;

public interface RMICounterService extends Rmi {

    static final long serialVersionUID = 2970373094765915499L;

    void addInCount(String cid, int count) throws RemoteException;

    void addOutCount(String cid, int count) throws RemoteException;

}
