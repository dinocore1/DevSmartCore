package com.devsmart;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;


public class Delegate<C> {

    public interface RegisterCallback<Q> {
        void onFirstListener(Q callback);
        void onLastListener(Q callback);
    }

    private final Class<C> mClassType;
    public RegisterCallback mOnRegisterCallback;

    public Delegate(Class<C> classType) {
        mClassType = classType;
    }

    private CopyOnWriteArraySet<C> mCallbacks = new CopyOnWriteArraySet<C>();

    public void registerCallback(C callback) {
        if(mOnRegisterCallback != null && mCallbacks.isEmpty()){
            mOnRegisterCallback.onFirstListener(callback);
        }
        mCallbacks.add(callback);
    }

    public void unregisterCallback(C callback) {
        mCallbacks.remove(callback);
        if(mOnRegisterCallback != null && mCallbacks.isEmpty()) {
            mOnRegisterCallback.onLastListener(callback);
        }
    }

    public void unregisterAllCallbacks() {
        mCallbacks.clear();
    }

    public void emit(String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?>[] params = new Class<?>[args.length];
        for(int i=0;i<args.length;i++){
            params[i] = args[i].getClass();
        }

        for(C cb : mCallbacks){
            Method method = cb.getClass().getMethod(methodName, params);
            method.invoke(cb, args);
        }
    }

    public C createInvoker() {
        return (C) Proxy.newProxyInstance(mClassType.getClassLoader(), new Class[]{mClassType}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                for(C cb : mCallbacks){
                    for(Method cm : cb.getClass().getMethods()){
                        if(cm.getName().equals(method.getName()) && Arrays.equals(cm.getParameterTypes(), method.getParameterTypes())){
                            method.invoke(cb, args);
                            break;
                        }
                    }
                }
                return null;
            }
        });
    }
}