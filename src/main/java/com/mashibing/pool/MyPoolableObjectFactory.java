package com.mashibing.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * description  MyPoolableObjectFactory <BR>
 * <p>
 * author: zhao.song
 * date: created in 11:31  2021/7/22
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class MyPoolableObjectFactory extends BasePooledObjectFactory<Resource> {

    /**
     * description   创建一个对象实例  <BR>
     *
     * @param :
     * @return {@link Resource}
     * @author zhao.song  2021/7/22  11:32
     */
    @Override
    public Resource create() throws Exception {
        return new Resource();
    }

    /**
     * description   包裹创建的对象实例,返回一个pooledobject  <BR>
     *
     * @param resource:
     * @return {@link PooledObject<Resource>}
     * @author zhao.song  2021/7/22  11:32
     */
    @Override
    public PooledObject<Resource> wrap(Resource resource) {
        return new DefaultPooledObject<Resource>(resource);
    }
}
