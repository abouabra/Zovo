import React from 'react'

const CheckEmailFullscreen = () => {
  return (
    <div className="absolute inset-0 z-10 flex flex-col items-center pt-32 bg-app-bg/100">
        <span className="text-2xl font-bold">
            We&apos;ve sent you an email to verify your account
        </span>
        <span className='text-lg text-muted-foreground'>
            Please check your inbox and follow the instructions to complete your registration.
        </span>
    </div>
  )
}

export default CheckEmailFullscreen