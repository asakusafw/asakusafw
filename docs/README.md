# Asakusa Framework Documentation

## How to Build

This documentation uses the [Sphinx](http://www.sphinx-doc.org) documentation system.

Building the documentation requires at least version 1.4 of [Sphinx](http://www.sphinx-doc.org) and [pygments-dmdl](https://pypi.python.org/pypi/pygments-dmdl) have to be installed.

```
pip install sphinx
pip install pygments-dmdl
```

After installing:

```
cd docs/ja
make html
```

Then, open your browser to ``build/html/index.html``.
