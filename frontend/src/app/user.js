import { createSlice } from '@reduxjs/toolkit'

const initialState = {
    userdata:{}
}

export const userSlice = createSlice({
  name: 'counter',
  initialState,
  reducers: {
    login: (state,action) => {
      state.userdata = action.payload
    },
    logout: (state) => {
      state.userdata = initialState.userdata
    },
  },
})

export const {login , logout} = userSlice.actions;