package com.unilim.iut.truckers.modele

data class NumeroTelephone(var numeroTelephone: String) {
    init {
        if (numeroTelephone.substring(0, 3) == "+33") {
            numeroTelephone = "0" + numeroTelephone.substring(3)
        }
        require(estUnNumeroFrancaisValide(numeroTelephone)) {
            "Le numéro de téléphone doit être au format international français : $numeroTelephone"
        }
    }

    private fun estUnNumeroFrancaisValide(numeroTelephone: String): Boolean {
        // Numéro de téléphone français au format international :
        //  0X XX XX XX XX (10 chiffres) avec X compris entre 0 et 9, sauf le premier qui doit valoir entre 1 et 9
        val regex = """0[1-9]\d{8}"""
        return numeroTelephone.matches(regex.toRegex())
    }
}
