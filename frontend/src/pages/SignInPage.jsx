import axios from 'axios';
import React, { useState } from 'react'
import { useDispatch } from 'react-redux';
import { store } from '../app/store';
import { login } from '../app/user';

export const SignInPage = () => {
  const[userfrom,setUserFrom] = useState({
    username : "",
    password : ""
  })
  const dispatch = useDispatch();
  const updateUserFrom =(e)=>{
    setUserFrom({...userfrom,[e.target.name] : e.target.value});
  }
  const loginUser = async (user)=>{
    try {
      const response = await axios.post(`http://localhost:8080/api/users/login`, userfrom, {
        headers: {
          "Content-Type": "application/json",
        },
      });
      if (response.status!=200) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      if (response.request.status==200) {
          const {jwt,user} =response.data;
          const userData = user;
          userData.token = jwt;
          await dispatch(login(userData));
          alert("Login succefully");
          // window.location.href = "/dashboard";
      } else {
        // setMessage(data.message || "Invalid credentials.");
      }
    } catch (error) {
      console.error("Login error:", error.message);
    }
  }
  function handleSubmit(e){
    e.preventDefault();
    loginUser(userfrom);
  }
  return (
    <>
<form className="form" onSubmit={handleSubmit}>
  <span className="input-span">
    <label for="username" className="label">Username :</label>
    <input type="text" name="username" id="username" onChange={updateUserFrom}/>
  </span>
  <span className="input-span">
    <label for="password" className="label">Password</label>
    <input type="password" name="password" id="password" onChange={updateUserFrom}/>
  </span>
  <span className="span"><a href="#">Forgot password?</a></span>
  <input className="submit" type="submit" value="Log in" />
  <span className="span">Don't have an account? <a href="#">Sign up</a></span>
</form>

    </>
  )
}
