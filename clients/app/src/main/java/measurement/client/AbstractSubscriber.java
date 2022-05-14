package measurement.client;

public abstract class AbstractSubscriber extends AbstractClient{
    public AbstractSubscriber(String clientId){
        super(clientId);
    }

    public abstract void subscribe();
}
