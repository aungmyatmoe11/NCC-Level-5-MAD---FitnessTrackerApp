package com.example.fitnesstrackingmobileapp

object EndPoints {
    private val URL_ROOT = "http://192.168.1.9/v1/?op="
    val URL_ADD_USER = URL_ROOT + "adduser"
    val URL_GET_USERS = URL_ROOT + "getusers"
    val URL_LOGIN_USER = URL_ROOT + "loginuser"
}