import io
import logging
import sys
from contextlib import redirect_stdout

from dbt.cli.main import dbtRunner, dbtRunnerResult
import argparse

if __name__ == "__main__":
    logger = logging.getLogger()
    logger.disabled = False

    parser = argparse.ArgumentParser(description="Example script with a mandatory name selection argument.")

    # Add the --select (-s) argument
    parser.add_argument('-s', '--select', required=True, help='Name to select', type=str)

    # Parse the command-line arguments
    args = parser.parse_args()

    # Print the provided name
    model_name = args.select
    print(model_name)
    try:
        # initialize
        dbt = dbtRunner()

        cli_args = ["compile", "--select", model_name, "--no-version-check",
                    "--no-introspect", "--quiet"]

        # dbt likes to print to stdout, so we need to capture it, but we don't really care about the output.
        buffer = io.StringIO()
        with redirect_stdout(buffer):
            res: dbtRunnerResult = dbt.invoke(cli_args)

        if res.result and len(res.result) >= 1 and res.result[0].node.compiled_path:
            print(res.result[0].node.compiled_path)
            sys.exit(0)
        else:
            import sys

            # Figure out what went wrong.
            # 1. No result?
            if not res.result:
                if res.exception:
                    print(res.exception)
                else:
                    print("No result?")
                sys.exit(1)
            # 2. No compiled_path?
            elif not res.result[0].node.compiled_path:
                print("No compiled_path")
                sys.exit(1)
            elif res.result[0].node.status == "error":
                print("Error: ", res.result[0].node.error)
                sys.exit(1)
            else:
                # 3. Something else?
                print("Unknown error")
                sys.exit(1)

    except Exception as e:
        print(e)
        sys.exit(1)
