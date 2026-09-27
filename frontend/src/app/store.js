import { configureStore } from '@reduxjs/toolkit'
import { userSlice } from './user.js'

export const store = configureStore({
  reducer: {
    user :userSlice.reducer,
  },
})