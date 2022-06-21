package com.koptao.serialportlibrary

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android_serialport_api.SerialPort
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Kongqw on 2017/11/13.
 * SerialPortManager
 */
class SerialPortManager : SerialPort() {

    private var mFileInputStream: FileInputStream? = null

    private var mFileOutputStream: FileOutputStream? = null

    private var mSendingHandlerThread: HandlerThread? = null

    private var mSendingHandler: Handler? = null

    private var mSerialPortReadThread: SerialPortReadThread? = null

    private var mOnSerialPortDataListener: OnSerialPortDataListener? = null

    fun openSerialPort(
        device: String,
        baudrate: BAUDRATE,
        stopbit: STOPB = STOPB.B1,
        databit: DATAB = DATAB.CS8,
        parity: PARITY = PARITY.NONE,
        flowCon: FLOWCON = FLOWCON.NONE
    ): Boolean {
        return try {
            val open = open(File(device), baudrate, stopbit, databit, parity, flowCon)
            if (open) {
                mFileInputStream = inputStream as FileInputStream
                mFileOutputStream = outputStream as FileOutputStream
                //开启发送消息的线程
                startSendThread()
                //开启接收消息的线程
                startReadThread()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 关闭串口
     */
    fun closeSerialPort() {
        close()
        //停止发送消息的线程
        stopSendThread()
        //停止接收消息的线程
        stopReadThread()

        try {
            mFileInputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mFileInputStream = null

        try {
            mFileOutputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mFileOutputStream = null

        mOnSerialPortDataListener = null
    }

    /**
     * 添加数据通信监听
     *
     * @param listener listener
     * @return SerialPortManager
     */
    fun setOnSerialPortDataListener(listener: OnSerialPortDataListener) {
        mOnSerialPortDataListener = listener
    }

    /**
     * 开启发送消息的线程
     */
    private fun startSendThread() {
        mSendingHandlerThread = HandlerThread("mSendingHandlerThread")
        mSendingHandlerThread?.apply {
            start()
            mSendingHandler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    val sendBytes = msg.obj as ByteArray
                    if (null != mFileOutputStream && sendBytes.isNotEmpty()) {
                        try {
                            mFileOutputStream?.apply {
                                write(sendBytes)
//                                write(10)
                            }
                            mOnSerialPortDataListener?.onDataSent(sendBytes)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    /**
     * 停止发送消息线程
     */
    private fun stopSendThread() {
        mSendingHandler?.removeCallbacksAndMessages(null)
        mSendingHandler = null
        if (null != mSendingHandlerThread) {
            mSendingHandlerThread?.apply {
                interrupt()
                quit()
            }
            mSendingHandlerThread = null
        }
    }

    /**
     * 开启接收消息的线程
     */
    private fun startReadThread() {
        mSerialPortReadThread = object : SerialPortReadThread(mFileInputStream!!) {
            override fun onDataReceived(bytes: ByteArray?) {
                mOnSerialPortDataListener?.onDataReceived(bytes)
            }
        }
        mSerialPortReadThread?.start()
    }

    /**
     * 停止接收消息的线程
     */
    private fun stopReadThread() {
        mSerialPortReadThread?.release()
    }

    /**
     * 发送数据
     *
     * @param cmd 发送数据
     * @return 发送是否成功
     */
    fun sendCmd(cmd: String): Boolean {
        if (null != mFileInputStream && null != mFileOutputStream) {
            val array = CharArray(cmd.length)
            for (i in cmd.indices) {
                array[i] = cmd[i]
            }
            val message = Message.obtain()
            message.obj = String(array).toByteArray()
            return mSendingHandler?.sendMessage(message) ?: false
        }
        return false
    }

    /**
     * 发送数据
     *
     * @param cmd 发送数据
     * @return 发送是否成功
     */
    fun sendCmd(cmd: ByteArray?): Boolean {
        if (null != mFileInputStream && null != mFileOutputStream) {
            val message = Message.obtain()
            message.obj = cmd
            return mSendingHandler?.sendMessage(message) ?: false
        }
        return false
    }
}