function validateForm() {
    const firstname = $("#firstName").val();//document.getElementById("firstName").value;
    const lastname =  $("#lastName").val();//document.getElementById("lastName").value;
    const password =  $("#password").val();//document.getElementById("password").value;
    const email = $("#mail").val();// document.getElementById("mail").value;

    console.log("firstname: " + firstname);
    console.log("lastname: " + lastname);
    console.log("password: " + password);
    console.log("email: " + email);

    const lastNamePattern = /^[A-Z]+$/;
    const firstNamePattern = /^[A-Z][a-z]+$/;
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,15}$/;
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!firstNamePattern.test(firstname)) {
        alert("Le nom doit être écrit en lettre majuscules");
        return false;
    }

    if (!lastNamePattern.test(lastname)) {
        alert("Le premom doit commencer par une lettre majuscule suivi de minuscules.");
        return false;
    }

    if (!passwordPattern.test(password)) {
        alert("Le mot de passe doit : \n contenir entre 8 et 15 caractères dont : \nune lettre majuscule, \nune lettre minuscule, \net un chiffre.");
        return false;
    }

    if (!emailPattern.test(email)) {
        alert("Email doit être de la forme exemple@mail.dom");
        return false;
    }

    return true;
}