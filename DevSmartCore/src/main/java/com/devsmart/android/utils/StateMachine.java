package com.devsmart.android.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class StateMachine<S extends Enum<?>, I extends Enum<?>> {

    public interface StateChangeListener<S extends Enum<?>, I extends Enum<?>> {
        void onStateChanged(StateMachine<S, I> stateMachine, S lastState, S newState, I input, Object data);
    }

    private class Transition {
        public final S startState;
        public final I input;

        private Transition(S startState, I input) {
            this.startState = startState;
            this.input = input;
        }

        @Override
        public boolean equals(Object o) {
            Transition other = (Transition)o;
            return startState == other.startState && input == other.input;
        }

        @Override
        public int hashCode() {
            return startState.hashCode() ^ input.hashCode();
        }
    }


    private HashMap<Transition, S> mTransisition = new HashMap<Transition, S>();
    private Set<StateChangeListener<S, I>> mListeners = new CopyOnWriteArraySet<StateChangeListener<S, I>>();
    private S mState;

    public StateMachine(S init) {
        mState = init;
    }

    public void configure(S startState, I input, S endState) {
        mTransisition.put(new Transition(startState, input), endState);
    }

    public void input(I input, Object data){
        Transition arc = new Transition(mState, input);
        S nextState = mTransisition.get(arc);
        if(nextState != null) {
            S oldState;
            synchronized (this) {
                oldState = mState;
                mState = nextState;
            }
            notifyStateChanged(oldState, mState, input, data);
        } else {
            Log.w("", String.format("no possible transition from: {} with input: {}", mState, input));
        }
    }

    private void notifyStateChanged(S oldState, S newState, I input, Object data) {
        for(StateChangeListener<S, I> listener : mListeners){
            listener.onStateChanged(this, oldState, newState, input, data);
        }
    }

    public void addListener(StateChangeListener<S, I> listener){
        mListeners.add(listener);
    }

    public void removeListener(StateChangeListener<S, I> listener){
        mListeners.remove(listener);
    }

    public synchronized S getState() {
        return mState;
    }
}