function validateResetForm() {
    const newPassword = $("#new_password").val();  //document.getElementById("new_password").value;
    const confirmPassword =  $("#confirm_password").val();  //document.getElementById("confirm_password").value;

    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,15}$/;

    if (newPassword !== confirmPassword) {
        alert("Les saisies sont différentes, réessayer");
        return false;
    }

    if (!passwordPattern.test(newPassword)) {
        alert("Le mot de passe doit : \n contenir entre 8 et 15 caractères dont : \nune lettre majuscule, \nune lettre minuscule, \net un chiffre.");
        return false;
    }
    return true;
}