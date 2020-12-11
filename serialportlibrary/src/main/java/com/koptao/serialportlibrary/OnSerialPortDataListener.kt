package com.koptao.serialportlibrary

/**
 * Created by Kongqw on 2017/11/14.
 * 串口消息监听
 */
interface OnSerialPortDataListener {

    /**
     * 数据接收
     *
     * @param bytes 接收到的数据
     */
    fun onDataReceived(bytes: ByteArray?)

    /**
     * 数据发送
     *
     * @param bytes 发送的数据
     */
    fun onDataSent(bytes: ByteArray?)
}