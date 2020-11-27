package org.github.toxrink.metric;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMICounterServiceImpl extends UnicastRemoteObject implements RMICounterService {

    private static final long serialVersionUID = -2892367551430892549L;

    protected RMICounterServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public void addInCount(String cid, int count) throws RemoteException {
        MetricUtils.getCounterByCid(cid).getInCounter().addAndGet(count);
    }

    @Override
    public void addOutCount(String cid, int count) throws RemoteException {
        MetricUtils.getCounterByCid(cid).getOutCounter().addAndGet(count);
    }

}
