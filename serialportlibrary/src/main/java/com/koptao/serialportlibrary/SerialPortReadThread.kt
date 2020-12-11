package com.koptao.serialportlibrary

import java.io.IOException
import java.io.InputStream

/**
 * Created by Kongqw on 2017/11/14.
 * 串口消息读取线程
 */
abstract class SerialPortReadThread(
    private val inputStream: InputStream,
    private val readBuffer: ByteArray = ByteArray(1024)
) : Thread() {

    abstract fun onDataReceived(bytes: ByteArray?)

    override fun run() {
        super.run()
        while (!isInterrupted) {
            try {
                val size: Int = inputStream.read(readBuffer)
                if (-1 == size || 0 >= size) {
                    return
                }
                val readBytes = ByteArray(size)
                System.arraycopy(readBuffer, 0, readBytes, 0, size)
                onDataReceived(readBytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
        }
    }

    @Synchronized
    override fun start() {
        super.start()
    }

    /**
     * 关闭线程 释放资源
     */
    fun release() {
        interrupt()
        try {
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}