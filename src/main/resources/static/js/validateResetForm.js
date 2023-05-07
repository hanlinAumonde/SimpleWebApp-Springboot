function validateResetForm() {
    const newPassword = $("#new_password").val();  //document.getElementById("new_password").value;
    const confirmPassword =  $("#confirm_password").val();  //document.getElementById("confirm_password").value;

    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,15}$/;

    if (newPassword !== confirmPassword) {
        alert("The passwords do not match.");
        return false;
    }

    if (!passwordPattern.test(newPassword)) {
        alert("Password must be between 8 and 15 characters long, and contain at least:\n one uppercase letter, \none lowercase letter, \nand one digit.");
        return false;
    }
    return true;
}