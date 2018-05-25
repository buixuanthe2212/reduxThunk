package com.jinjerkeihi.scancard.api;

/**
 * Description
 *
 * @author Pika.
 */
public class Presenter<V> {

    private V view;

    public void bindView(V view) {
        this.view = view;
    }

    public V view() {
        return view;
    }

    public void unBindView() {
        this.view = null;
    }

}
